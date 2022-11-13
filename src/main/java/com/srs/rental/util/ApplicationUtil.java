package com.srs.rental.util;

import com.srs.common.util.PermissionUtil;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.entity.ApplicationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Log4j2
public class ApplicationUtil {

    public boolean isNotMyApplication(ApplicationEntity application, GrpcPrincipal principal) {
        return !PermissionUtil.isPublicUser(principal.getRoles())
                || (!principal.getUserId().equals(application.getCreatedBy()) && !principal.getEmail().equalsIgnoreCase(application.getOwner().getEmail()));
    }


}
