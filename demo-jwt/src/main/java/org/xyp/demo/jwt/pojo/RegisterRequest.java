package org.xyp.demo.jwt.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    private String email;
    private String password;

}
