package mz.org.fgh.sifmoz.backend.reports.common;

class ReportProcessStatus {
    private String reportId;
    private String msg;
    private double progress;

    public ReportProcessStatus() {
    }

    public ReportProcessStatus(String reportId, String msg, int progress) {
        this.reportId = reportId;
        this.msg = msg;
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "{" +
                "reportId='" + reportId + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
