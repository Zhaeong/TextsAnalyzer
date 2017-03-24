package com.example.owen.textsanalyzer.helperConstructs;

/**
 * Created by Owen on 2017-03-23.
 */

public class ContactObj {
    public Long ContactId;
    public String name;
    public String number;

    public ContactObj(Long ContactId, String name, String number)
    {
        this.ContactId = ContactId;
        this.name = name;
        this.number = number;
    }
}
