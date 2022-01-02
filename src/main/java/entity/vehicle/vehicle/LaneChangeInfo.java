package entity.vehicle.vehicle;

public class LaneChangeInfo {
    private int partnerType; // 0: 无 shadow, 1：有 shadow 且是原车, 2：为 shadow
    private Vehicle partner;
    private double offSet; // laneChange 时变道距离判断
    private int segmentIndex;

    LaneChangeInfo() {
    }

    LaneChangeInfo(LaneChangeInfo laneChangeInfo) {
        partnerType = laneChangeInfo.partnerType;
        partner = laneChangeInfo.partner;
        segmentIndex = laneChangeInfo.segmentIndex;
        offSet = laneChangeInfo.offSet;
    }

    public int getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(int partnerType) {
        this.partnerType = partnerType;
    }

    public Vehicle getPartner() {
        return partner;
    }

    public void setPartner(Vehicle partner) {
        this.partner = partner;
    }

    public double getOffSet() {
        return offSet;
    }

    public void setOffSet(double offSet) {
        this.offSet = offSet;
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }
}
