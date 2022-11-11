package com.srs.rental.util.validator;

import com.srs.common.NoContentResponse;
import com.srs.rental.Application;
import com.srs.rental.CheckExistApplicationRequest;
import com.srs.rental.SubmitApplicationRequest;

public interface ApplicationValidator {
    NoContentResponse validateSubmitApplication(SubmitApplicationRequest request);

    NoContentResponse validateSubmitApplication(Application request);

    NoContentResponse validateCheckExistApplication(CheckExistApplicationRequest request);

}
