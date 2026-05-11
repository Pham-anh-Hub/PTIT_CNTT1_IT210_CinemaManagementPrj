package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MonthRevenueDTO {
    private int month;
    private BigDecimal revenue;
    private int percentToMax;
    private String monthName;
}


