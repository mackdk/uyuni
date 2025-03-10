# Copyright (c) 2017-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete Red Hat-like minion and register as SSH minion
# 2) run a remote command
# 3) delete Red Hat-like SSH minion and register as normal minion
#
# This feature can cause failures in the following features:
# - features/secondary/min_rhlike_salt_install_package_and_patch.feature
# - features/secondary/min_rhlike_salt_install_with_staging.feature
# - features/secondary/min_rhlike_monitoring.feature
# If the cleanup bootstrap scenario fails,
# the minion will not be reachable in those features

@skip_if_github_validation
@scope_res
@rhlike_minion
Feature: Bootstrap a SSH-managed Red Hat-like minion and do some basic operations on it

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Delete the Red Hat-like minion before SSH minion tests
    When I am on the Systems overview page of this "rhlike_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "rhlike_minion"
    Then "rhlike_minion" should not be registered

  Scenario: Bootstrap a SSH-managed Red Hat-like minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "rhlike_minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "rhlike_minion", refreshing the page
    And I wait until onboarding is completed for "rhlike_minion"

@proxy
  Scenario: Check connection from SSH-managed Red Hat-like minion to proxy
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SSH-managed Red Hat-like minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rhlike_minion" hostname

  Scenario: Subscribe the SSH-managed Red Hat-like minion to a base channel
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake-Base-Channel-RH-like"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

  Scenario: Check events history for failures on SSH-managed Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    Then I check for failed events on history event page

  Scenario: Run a remote command on the SSH-managed Red Hat-like minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "rhlike_minion"
    And I click on preview
    And I click on run
    Then I should see "rhlike_minion" hostname
    When I wait for "15" seconds
    And I expand the results for "rhlike_minion"
    Then I should see a "rhel centos fedora" text
    And I should see a "ROCKY_SUPPORT_PRODUCT" text

  Scenario: Check events history for failures on SSH-managed Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    Then I check for failed events on history event page

  Scenario: Cleanup: delete the SSH-managed Red Hat-like minion
    When I am on the Systems overview page of this "rhlike_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "rhlike_minion" should not be registered

  Scenario: Cleanup: bootstrap a Red Hat-like minion after SSH minion tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rhlike_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "rhlike_minion", refreshing the page
    And I wait until onboarding is completed for "rhlike_minion"

  Scenario: Cleanup: re-subscribe the Red Hat-like minion to a base channel
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake-Base-Channel-RH-like"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed
