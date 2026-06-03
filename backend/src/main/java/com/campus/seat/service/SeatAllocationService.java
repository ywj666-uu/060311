package com.campus.seat.service;

import com.campus.seat.algorithm.SeatAllocator;
import com.campus.seat.dto.SeatMapResponse;
import com.campus.seat.entity.*;
import com.campus.seat.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatAllocationService {

    private static final String SEAT_MAP_CACHE_PREFIX = "seatmap:";
    private static final String SEAT_STATUS_PREFIX = "seat:status:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final RegistrationRepository registrationRepository;
    private final SeatRepository seatRepository;
    private final ActivityRepository activityRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SeatAllocator seatAllocator;
    private final RedisLockService redisLockService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public SeatAllocationService(RegistrationRepository registrationRepository,
                                 SeatRepository seatRepository,
                                 ActivityRepository activityRepository,
                                 VenueRepository venueRepository,
                                 UserRepository userRepository,
                                 TeamRepository teamRepository,
                                 SeatAllocator seatAllocator,
                                 RedisLockService redisLockService,
                                 RedisTemplate<String, Object> redisTemplate,
                                 ObjectMapper objectMapper) {
        this.registrationRepository = registrationRepository;
        this.seatRepository = seatRepository;
        this.activityRepository = activityRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.seatAllocator = seatAllocator;
        this.redisLockService = redisLockService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<SeatAllocator.AllocationResult> allocateSeats(Long activityId) {
        String lockKey = "lock:seat-alloc:" + activityId;
        boolean locked = redisLockService.tryLock(lockKey, 30);
        if (!locked) {
            throw new RuntimeException("无法获取分配锁，请稍后重试");
        }

        try {
            Activity activity = activityRepository.findById(activityId)
                    .orElseThrow(() -> new RuntimeException("活动不存在"));
            Venue venue = venueRepository.findById(activity.getVenueId())
                    .orElseThrow(() -> new RuntimeException("场地不存在"));

            List<Registration> pendingRegs = registrationRepository
                    .findByActivityIdAndStatusOrderByRegistrationTimeAsc(activityId, "PENDING");

            if (pendingRegs.isEmpty()) {
                return Collections.emptyList();
            }

            List<Seat> allSeats = seatRepository.findByVenueIdOrderByRowNumAscColNumAsc(venue.getId());

            Set<Long> allocatedSeatIds = registrationRepository
                    .findByActivityIdOrderByRegistrationTimeAsc(activityId).stream()
                    .filter(r -> r.getAllocatedSeatId() != null && !"CANCELLED".equals(r.getStatus()))
                    .map(Registration::getAllocatedSeatId)
                    .collect(Collectors.toSet());

            List<SeatAllocator.AllocationResult> results = seatAllocator.allocate(
                    pendingRegs, allSeats, allocatedSeatIds,
                    venue.getTotalRows(), venue.getTotalCols(),
                    Boolean.TRUE.equals(venue.getHasWindowLeft()),
                    Boolean.TRUE.equals(venue.getHasWindowRight()));

            for (SeatAllocator.AllocationResult result : results) {
                Registration reg = registrationRepository.findById(result.getRegistrationId()).orElse(null);
                if (reg != null) {
                    reg.setAllocatedSeatId(result.getSeatId());
                    reg.setStatus("ALLOCATED");
                    registrationRepository.save(reg);
                }
            }

            // 同步更新Redis缓存
            refreshRedisCache(activityId);
            return results;
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    public SeatMapResponse getSeatMap(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        Venue venue = venueRepository.findById(activity.getVenueId())
                .orElseThrow(() -> new RuntimeException("场地不存在"));

        List<Seat> seats = seatRepository.findByVenueIdOrderByRowNumAscColNumAsc(venue.getId());
        List<Registration> allocations = registrationRepository
                .findByActivityIdOrderByRegistrationTimeAsc(activityId).stream()
                .filter(r -> r.getAllocatedSeatId() != null && !"CANCELLED".equals(r.getStatus()))
                .collect(Collectors.toList());

        Map<Long, Registration> seatToReg = allocations.stream()
                .collect(Collectors.toMap(Registration::getAllocatedSeatId, r -> r, (a, b) -> a));

        List<SeatMapResponse.SeatInfo> seatInfos = new ArrayList<>();
        for (Seat seat : seats) {
            Registration reg = seatToReg.get(seat.getId());
            String status = "AVAILABLE";
            String studentName = null;
            String studentId = null;
            Long teamId = null;
            String teamName = null;
            String preferredArea = null;
            boolean preferenceMatched = false;

            String areaTag = SeatAllocator.computeArea(
                    seat.getRowNum(), seat.getColNum(),
                    venue.getTotalRows(), venue.getTotalCols(),
                    Boolean.TRUE.equals(venue.getHasWindowLeft()),
                    Boolean.TRUE.equals(venue.getHasWindowRight()));

            if (reg != null) {
                status = "ALLOCATED";
                User user = userRepository.findById(reg.getUserId()).orElse(null);
                if (user != null) {
                    studentName = user.getName();
                    studentId = user.getStudentId();
                }
                teamId = reg.getTeamId();
                if (teamId != null) {
                    Team team = teamRepository.findById(teamId).orElse(null);
                    if (team != null) {
                        teamName = team.getName();
                    }
                }
                preferredArea = reg.getPreferredArea();
                preferenceMatched = SeatAllocator.isPreferenceMatched(preferredArea, areaTag);
            }

            if (!Boolean.TRUE.equals(seat.getIsAvailable())) {
                status = "UNAVAILABLE";
            }

            seatInfos.add(new SeatMapResponse.SeatInfo(
                    seat.getId(), seat.getRowNum(), seat.getColNum(),
                    areaTag, status, studentName, studentId, teamId, teamName,
                    preferredArea, preferenceMatched
            ));
        }

        return new SeatMapResponse(
                activityId, activity.getTitle(), venue.getName(),
                venue.getTotalRows(), venue.getTotalCols(),
                venue.getHasWindowLeft(), venue.getHasWindowRight(),
                seatInfos
        );
    }

    /**
     * 管理员手动调整座位：更新数据库 + 刷新Redis缓存 + 返回最新座位图
     */
    @Transactional
    public SeatMapResponse adjustSeat(Long registrationId, Long newSeatId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));

        List<Registration> allRegs = registrationRepository
                .findByActivityIdOrderByRegistrationTimeAsc(reg.getActivityId());
        boolean seatTaken = allRegs.stream()
                .anyMatch(r -> newSeatId.equals(r.getAllocatedSeatId())
                        && !r.getId().equals(registrationId)
                        && !"CANCELLED".equals(r.getStatus()));

        if (seatTaken) {
            throw new RuntimeException("目标座位已被占用");
        }

        // 更新数据库
        reg.setAllocatedSeatId(newSeatId);
        reg.setStatus("ALLOCATED");
        registrationRepository.save(reg);

        // 同步刷新Redis缓存
        refreshRedisCache(reg.getActivityId());

        // 返回最新座位图供前端强制刷新
        return getSeatMap(reg.getActivityId());
    }

    /**
     * 管理员交换座位：更新数据库 + 刷新Redis缓存 + 返回最新座位图
     */
    @Transactional
    public SeatMapResponse swapSeats(Long regId1, Long regId2) {
        Registration reg1 = registrationRepository.findById(regId1)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));
        Registration reg2 = registrationRepository.findById(regId2)
                .orElseThrow(() -> new RuntimeException("报名记录不存在"));

        // 交换数据库中的座位
        Long tempSeat = reg1.getAllocatedSeatId();
        reg1.setAllocatedSeatId(reg2.getAllocatedSeatId());
        reg2.setAllocatedSeatId(tempSeat);

        registrationRepository.save(reg1);
        registrationRepository.save(reg2);

        // 同步刷新Redis缓存
        refreshRedisCache(reg1.getActivityId());

        return getSeatMap(reg1.getActivityId());
    }

    /**
     * 刷新Redis中的座位状态缓存和座位图缓存
     */
    private void refreshRedisCache(Long activityId) {
        try {
            // 1. 删除旧的座位图缓存
            redisTemplate.delete(SEAT_MAP_CACHE_PREFIX + activityId);

            // 2. 更新逐座位状态哈希
            String statusKey = SEAT_STATUS_PREFIX + activityId;
            redisTemplate.delete(statusKey);

            List<Registration> allRegs = registrationRepository
                    .findByActivityIdOrderByRegistrationTimeAsc(activityId).stream()
                    .filter(r -> r.getAllocatedSeatId() != null && !"CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());

            Map<String, Object> statusMap = new HashMap<>();
            for (Registration reg : allRegs) {
                User user = userRepository.findById(reg.getUserId()).orElse(null);
                String value = user != null ? user.getStudentId() + ":" + user.getName() : "OCCUPIED";
                statusMap.put(String.valueOf(reg.getAllocatedSeatId()), value);
            }

            if (!statusMap.isEmpty()) {
                redisTemplate.opsForHash().putAll(statusKey, statusMap);
                redisTemplate.expire(statusKey, CACHE_TTL);
            }

            // 3. 重新生成并缓存完整座位图JSON
            SeatMapResponse seatMap = getSeatMap(activityId);
            String json = objectMapper.writeValueAsString(seatMap);
            redisTemplate.opsForValue().set(SEAT_MAP_CACHE_PREFIX + activityId, json, CACHE_TTL);
        } catch (Exception e) {
            // Redis不可用时降级，不阻断主流程
        }
    }
}
