package com.skillhub.backend.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CompanyPost {
    private String name;
    private String address;
    private String phone;
}
