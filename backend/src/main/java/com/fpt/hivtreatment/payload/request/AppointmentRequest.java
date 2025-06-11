package com.fpt.hivtreatment.payload.request;

import lombok.Data;
<<<<<<< HEAD

=======
import java.time.LocalDate;
>>>>>>> fd42c148e0431975301ca683137e9cc7dea64a1c

@Data
public class AppointmentRequest {
    private Long patientId;
    private Long doctorId;
    private Long appointmentSlotId;
    private String appointmentType;
    private Boolean isAnonymous = false;
    private String symptoms;
    private String notes;
    private String appointmentDate;
}