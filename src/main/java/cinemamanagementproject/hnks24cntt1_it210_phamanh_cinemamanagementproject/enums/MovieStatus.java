package cinemamanagementproject.hnks24cntt1_it210_phamanh_cinemamanagementproject.enums;

public enum MovieStatus {
    NOW_SHOWING("Đang chiếu"),
    UPCOMING("Sắp chiếu"),
    ENDED("Ngừng chiếu");

    private final String label;

    MovieStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
