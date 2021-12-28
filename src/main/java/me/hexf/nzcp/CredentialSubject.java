package me.hexf.nzcp;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class CredentialSubject {
    private final String givenName;
    private final String familyName;
    private final LocalDate dateOfBirth;

    public CredentialSubject(String givenName, String familyName, LocalDate dateOfBirth){
        this.givenName = givenName;
        this.familyName = familyName;
        this.dateOfBirth = dateOfBirth;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialSubject that = (CredentialSubject) o;
        return Objects.equals(givenName, that.givenName) && Objects.equals(familyName, that.familyName) && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(givenName, familyName, dateOfBirth);
    }
}
