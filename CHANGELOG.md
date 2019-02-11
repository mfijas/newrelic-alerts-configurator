# 3.3.2 (08-02-2018)
- Improved json deserialization in Mail Channel

# 3.3.1 (03-10-2018)
- Added more "Since" values to NRQL Condition

# 3.3.0 (07-08-2018)
- Added support for Synthetics Monitor Failure conditions
- Removed unused AlertsNrqlConditionsApi interface

# 3.2.0 (09.04.2018)
- Added new GcMetric `GC/ParNew` and `GC/ConcurrentMarkSweep` 

# 3.1.0 (23.02.2018)
- Added support for New Relic Browser conditions 

# 3.0.0 (17.01.2018)
- Added support for NRQL conditions
- Changed conditions and channels management logic. Changed default conditions state. Those are breaking changes - see details in 
[Migration guide](MIGRATION.md)

# 2.0.3 (06.12.2017)
- Added support for JVM metrics conditions

# 2.0.2 (20.09.2017)
- Add violation_close_timer
- Make incident preference required in policy configuration

# 2.0.0 (15.02.2017)
- Changed groupdId and artifactId
- Changed packages to `com.ocadotechnology.newrelic`
- Changed `AlertsChannelConfiguration` field `payload` to type `Object`
- JSON deserialization configured to convert empty string into null POJO reference 

# 1.4.0 (06.02.2017)
- Added support for user notification channel 
- Simplified channel synchronization logic

# 1.3.0 (24.01.2017)
- Added UserDefined condition type support for `ApmAppCondition` and `ServersMetricCondition`

# 1.2.0 (19.01.2017)
- Added PagerDuty channel support

# 1.1.2
- Added Webhook channel support

# 1.1.1
- fixed Maven publication problems

# 1.1.0
- added support for server metric conditions
- fixed type of `links` property in `KeyTransaction` 
