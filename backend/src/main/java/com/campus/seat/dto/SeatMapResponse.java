package com.campus.seat.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatMapResponse {
    private Long activityId;
    private String activityTitle;
    private String venueName;
    private Integer totalRows;
    private Integer totalCols;
    private Boolean hasWindowLeft;
    private Boolean hasWindowRight;
    private List<SeatInfo> seats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeatInfo {
        private Long seatId;
        private Integer rowNum;
        private Integer colNum;
        private String areaTag;
        private String status;
        private String studentName;
        private String studentId;
        private Long teamId;
        private String teamName;
        /** 学生选择的偏好区域 */
        private String preferredArea;
        /** 偏好是否命中当前座位区域 */
        private boolean preferenceMatched;
    }
}
