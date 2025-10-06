package org.kergru.library.model;

import java.util.List;

public record UserDto(
    String userName,
    String firstName,
    String lastName,
    String email,
    List<LoanDto> loans
) {

}
