package com.campus.seat.algorithm;

import com.campus.seat.entity.Registration;
import com.campus.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SeatAllocator {

    @Data
    @AllArgsConstructor
    public static class AllocationResult {
        private Long registrationId;
        private Long seatId;
    }

    /**
     * 根据物理行号判断区域，明确行号范围：
     * - 前排：第1行 ~ 第 ceil(totalRows/3) 行
     * - 中间：第 ceil(totalRows/3)+1 行 ~ 第 totalRows - floor(totalRows/3) 行
     * - 后排：第 totalRows - floor(totalRows/3)+1 行 ~ 第 totalRows 行
     * - 靠窗：第1列 或 第totalCols列（优先于排区判断）
     */
    public static String computeArea(int rowNum, int colNum, int totalRows, int totalCols,
                                     boolean hasWindowLeft, boolean hasWindowRight) {
        if (hasWindowLeft && colNum == 1) return "WINDOW";
        if (hasWindowRight && colNum == totalCols) return "WINDOW";

        int frontEnd = (int) Math.ceil(totalRows / 3.0);       // 5排时 = 2
        int backStart = totalRows - (totalRows / 3) + 1;       // 5排时 = 4

        if (rowNum <= frontEnd) return "FRONT";
        if (rowNum >= backStart) return "BACK";
        return "MIDDLE";
    }

    /**
     * 判断偏好是否命中
     */
    public static boolean isPreferenceMatched(String preference, String actualArea) {
        if (preference == null || "ANY".equals(preference)) return true;
        return preference.equals(actualArea);
    }

    /**
     * 严格按报名时间顺序分配座位。
     * 团队连座支持跨排相邻（上下左右接触均视为连座）。
     */
    public List<AllocationResult> allocate(List<Registration> registrations,
                                           List<Seat> allSeats,
                                           Set<Long> alreadyAllocatedSeatIds,
                                           int totalRows, int totalCols,
                                           boolean hasWindowLeft, boolean hasWindowRight) {
        List<AllocationResult> results = new ArrayList<>();
        Set<Long> usedSeatIds = new HashSet<>(alreadyAllocatedSeatIds);
        Set<Long> processedRegIds = new HashSet<>();

        List<Seat> available = allSeats.stream()
                .filter(s -> !usedSeatIds.contains(s.getId()) && Boolean.TRUE.equals(s.getIsAvailable()))
                .sorted(Comparator.comparingInt(Seat::getRowNum).thenComparingInt(Seat::getColNum))
                .collect(Collectors.toCollection(ArrayList::new));

        Map<Long, List<Registration>> teamMap = new LinkedHashMap<>();
        for (Registration reg : registrations) {
            if (reg.getTeamId() != null) {
                teamMap.computeIfAbsent(reg.getTeamId(), k -> new ArrayList<>()).add(reg);
            }
        }

        for (Registration reg : registrations) {
            if (processedRegIds.contains(reg.getId())) continue;

            if (reg.getTeamId() != null) {
                List<Registration> teamMembers = teamMap.get(reg.getTeamId()).stream()
                        .filter(r -> !processedRegIds.contains(r.getId()))
                        .collect(Collectors.toList());

                if (teamMembers.isEmpty()) continue;

                String teamPreference = majorityPreference(teamMembers);
                int teamSize = teamMembers.size();

                // 跨排相邻连座分配：先在偏好区域找，再全局找
                List<Seat> block = findAdjacentBlock(available, teamSize, teamPreference,
                        totalRows, totalCols, hasWindowLeft, hasWindowRight);
                if (block == null) {
                    block = findAdjacentBlock(available, teamSize, null,
                            totalRows, totalCols, hasWindowLeft, hasWindowRight);
                }
                if (block == null && available.size() >= teamSize) {
                    block = new ArrayList<>(available.subList(0, teamSize));
                }

                if (block != null) {
                    for (int i = 0; i < teamMembers.size() && i < block.size(); i++) {
                        Registration member = teamMembers.get(i);
                        Seat seat = block.get(i);
                        results.add(new AllocationResult(member.getId(), seat.getId()));
                        usedSeatIds.add(seat.getId());
                        processedRegIds.add(member.getId());
                    }
                    available.removeAll(block);
                }
            } else {
                Seat seat = findBestSeatInArea(available, reg.getPreferredArea(),
                        totalRows, totalCols, hasWindowLeft, hasWindowRight);
                if (seat == null && !available.isEmpty()) {
                    seat = available.get(0);
                }
                if (seat != null) {
                    results.add(new AllocationResult(reg.getId(), seat.getId()));
                    usedSeatIds.add(seat.getId());
                    available.remove(seat);
                }
                processedRegIds.add(reg.getId());
            }
        }

        return results;
    }

    /**
     * 寻找跨排相邻的连座块（BFS扩展，上下左右接触都视为连座）。
     * 优先使用刚好满足大小的紧凑块，避免浪费大段空位。
     */
    private List<Seat> findAdjacentBlock(List<Seat> available, int count, String preference,
                                         int totalRows, int totalCols,
                                         boolean hasWindowLeft, boolean hasWindowRight) {
        // 构建坐标到座位的映射
        Map<String, Seat> coordMap = new HashMap<>();
        List<Seat> candidates = new ArrayList<>();

        for (Seat s : available) {
            String key = s.getRowNum() + "," + s.getColNum();
            coordMap.put(key, s);
            if (preference == null || "ANY".equals(preference)) {
                candidates.add(s);
            } else {
                String area = computeArea(s.getRowNum(), s.getColNum(),
                        totalRows, totalCols, hasWindowLeft, hasWindowRight);
                if (preference.equals(area)) {
                    candidates.add(s);
                }
            }
        }

        if (candidates.size() < count) return null;

        // 对每个候选座位进行BFS，寻找包含count个相邻座位的连通块
        Set<String> candidateCoords = candidates.stream()
                .map(s -> s.getRowNum() + "," + s.getColNum())
                .collect(Collectors.toSet());

        List<Seat> bestBlock = null;
        int bestSpread = Integer.MAX_VALUE; // 最紧凑的块

        Set<String> visited = new HashSet<>();

        for (Seat start : candidates) {
            String startKey = start.getRowNum() + "," + start.getColNum();
            if (visited.contains(startKey)) continue;

            // BFS找到以start为起点的连通分量
            List<Seat> component = bfsComponent(start, candidateCoords, coordMap);
            for (Seat s : component) {
                visited.add(s.getRowNum() + "," + s.getColNum());
            }

            if (component.size() < count) continue;

            // 从连通分量中选出count个紧凑的相邻子集
            List<Seat> block = extractCompactSubset(component, count, coordMap, candidateCoords);
            if (block != null) {
                int spread = computeSpread(block);
                if (spread < bestSpread) {
                    bestSpread = spread;
                    bestBlock = block;
                }
            }
        }

        return bestBlock;
    }

    /**
     * BFS找连通分量（上下左右相邻）
     */
    private List<Seat> bfsComponent(Seat start, Set<String> candidateCoords, Map<String, Seat> coordMap) {
        List<Seat> component = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        Set<String> seen = new HashSet<>();
        String startKey = start.getRowNum() + "," + start.getColNum();
        queue.add(startKey);
        seen.add(startKey);

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            String key = queue.poll();
            Seat s = coordMap.get(key);
            if (s != null) component.add(s);

            String[] parts = key.split(",");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            for (int[] d : dirs) {
                String nk = (row + d[0]) + "," + (col + d[1]);
                if (candidateCoords.contains(nk) && !seen.contains(nk)) {
                    seen.add(nk);
                    queue.add(nk);
                }
            }
        }
        return component;
    }

    /**
     * 从连通分量中提取count个紧凑的相邻子集（BFS方式逐步扩展）
     */
    private List<Seat> extractCompactSubset(List<Seat> component, int count,
                                            Map<String, Seat> coordMap, Set<String> candidateCoords) {
        // 尝试从不同起点BFS取count个，选最紧凑的
        List<Seat> best = null;
        int bestSpread = Integer.MAX_VALUE;

        for (Seat start : component) {
            List<Seat> subset = new ArrayList<>();
            Queue<Seat> queue = new LinkedList<>();
            Set<String> seen = new HashSet<>();

            queue.add(start);
            seen.add(start.getRowNum() + "," + start.getColNum());

            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

            while (!queue.isEmpty() && subset.size() < count) {
                Seat cur = queue.poll();
                subset.add(cur);

                for (int[] d : dirs) {
                    String nk = (cur.getRowNum() + d[0]) + "," + (cur.getColNum() + d[1]);
                    if (candidateCoords.contains(nk) && !seen.contains(nk)) {
                        seen.add(nk);
                        Seat neighbor = coordMap.get(nk);
                        if (neighbor != null && component.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }

            if (subset.size() == count) {
                int spread = computeSpread(subset);
                if (spread < bestSpread) {
                    bestSpread = spread;
                    best = new ArrayList<>(subset);
                }
            }
        }
        return best;
    }

    /**
     * 计算座位块的散布度（越小越紧凑）
     */
    private int computeSpread(List<Seat> block) {
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
        for (Seat s : block) {
            minRow = Math.min(minRow, s.getRowNum());
            maxRow = Math.max(maxRow, s.getRowNum());
            minCol = Math.min(minCol, s.getColNum());
            maxCol = Math.max(maxCol, s.getColNum());
        }
        return (maxRow - minRow) + (maxCol - minCol);
    }

    /**
     * 为单人在偏好区域找最佳座位（优先不破坏连续空位段）
     */
    private Seat findBestSeatInArea(List<Seat> available, String preference,
                                    int totalRows, int totalCols,
                                    boolean hasWindowLeft, boolean hasWindowRight) {
        List<Seat> matching = new ArrayList<>();
        for (Seat s : available) {
            String area = computeArea(s.getRowNum(), s.getColNum(),
                    totalRows, totalCols, hasWindowLeft, hasWindowRight);
            if (preference == null || "ANY".equals(preference) || preference.equals(area)) {
                matching.add(s);
            }
        }
        if (matching.isEmpty()) return null;

        // 优先选连续空位段边缘的座位（不打断中间大段）
        Map<Integer, List<Seat>> rowMap = available.stream()
                .collect(Collectors.groupingBy(Seat::getRowNum));

        Seat best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Seat candidate : matching) {
            List<Seat> rowSeats = rowMap.get(candidate.getRowNum());
            if (rowSeats == null) continue;
            rowSeats.sort(Comparator.comparingInt(Seat::getColNum));
            int idx = rowSeats.indexOf(candidate);
            int score = Math.min(idx, rowSeats.size() - 1 - idx);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best != null ? best : matching.get(0);
    }

    private String majorityPreference(List<Registration> members) {
        Map<String, Long> counts = members.stream()
                .filter(r -> r.getPreferredArea() != null && !"ANY".equals(r.getPreferredArea()))
                .collect(Collectors.groupingBy(Registration::getPreferredArea, Collectors.counting()));
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("ANY");
    }
}
