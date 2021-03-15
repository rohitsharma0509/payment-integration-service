@functional
Feature: Generate and upload s1 file

  Scenario: Generate s1 file
    Given Set POST Generate s1 file service api endpoint
    #When Send a POST HTTP request for Generate s1 file
    #Then I receive valid HTTP Response Code 201 for Generate s1 file

  Scenario: Upload S1 file to sftp
    Given Set POST Upload S1 file to sftp service api endpoint
    #When Send a POST HTTP request for Upload S1 file to sftp
    #Then I receive valid HTTP Response Code 200 for Upload S1 file to sftp
