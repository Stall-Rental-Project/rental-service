package com.srs.rental.util;

import com.srs.common.util.TimestampUtil;
import com.srs.market.StallType;
import com.srs.rental.common.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;


@Component
@RequiredArgsConstructor
public class LeaseUtil {
    public static void main(String[] args) {
        var instance = new LeaseUtil();

        var now = TimestampUtil.now();
        var nowDefault = TimestampUtil.nowAtDefaultTimezone();

        System.out.println(now);
        System.out.println(instance.asLeaseStartDate(now));
        System.out.println(instance.asLeaseEndDate(now));

        System.out.println("====");
        System.out.println(nowDefault);
        System.out.println(instance.asLeaseStartDate(nowDefault));
        System.out.println(instance.asLeaseEndDate(nowDefault));

        System.out.println("====");
        System.out.println(now + " - " + now.withOffsetSameInstant(TimestampUtil.DEFAULT_OFFSET));
    }

    public OffsetDateTime asLeaseEndDate(String timestampString) {
        var timestamp = TimestampUtil.parseDatetimeString(timestampString);
        return this.asLeaseEndDate(timestamp);
    }

    public OffsetDateTime asLeaseStartDate(OffsetDateTime timestamp) {
        return TimestampUtil.beginningOfDate(timestamp.withOffsetSameLocal(TimestampUtil.DEFAULT_OFFSET));
    }

    public OffsetDateTime calcLeaseEndDate(OffsetDateTime leaseStartDate, int stallType) {
        return stallType == StallType.STALL_TYPE_PERMANENT_VALUE
                ? this.asLeaseEndDate(leaseStartDate.plusYears(Constant.PERMANENT_STALL_LEASE_DURATION))
                : this.asLeaseEndDate(leaseStartDate.plusYears(Constant.TEMPORARY_STALL_LEASE_DURATION));
    }

    public OffsetDateTime calcLeaseStartDate(OffsetDateTime leaseEndDate, int stallType) {
        return stallType == StallType.STALL_TYPE_PERMANENT_VALUE
                ? this.asLeaseStartDate(leaseEndDate.minusYears(Constant.PERMANENT_STALL_LEASE_DURATION))
                : this.asLeaseStartDate(leaseEndDate.minusYears(Constant.TEMPORARY_STALL_LEASE_DURATION));
    }

    public OffsetDateTime asLeaseEndDate(OffsetDateTime timestamp) {
        return TimestampUtil.endOfDate(timestamp.withOffsetSameLocal(TimestampUtil.DEFAULT_OFFSET));
    }
}
