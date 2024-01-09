package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class userDataDto extends Dto{

    long id;
    String firstName;
    String lastName;
    String userName;
    String mailAddress;
    boolean admin;
}
