Frontend service for the Guaranteed Minimum Pension service
===========================================================

This service provides the frontend application for the Guaranteed Minimum Pension service.

Summary
-----------

This service is designed for Pension Scheme Administrators and Pension Scheme Practitioners to request a [GMP] calculation
for a member of a contracted-out scheme under the following circumstances:

* The member is reaching State Pension Age
* The member is leaving the scheme
* The member has died and the surviving spouse requires a statement
* The member is transferring to another scheme
* The member is getting divorced


Requirements
------------

This service is written in [Scala] and [Play], so needs at least a [JRE] to run.


Authentication
------------

This user logs into this service using the [Government Gateway]


Acronyms
--------

In the context of this service we use the following acronyms:

* [API]: Application Programming Interface

* [HoD]: Head of Duty

* [JRE]: Java Runtime Environment

* [JSON]: JavaScript Object Notation

* [NINO]: National Insurance number

* [SCON]: Scheme Contracted

* [PSA]: Pension Scheme Administrator

* PSP: Pension Scheme Practitioner

* [SPA]: State Pension age

* [NPS]: National Insurance and PAYE System

* [URL]: Uniform Resource Locator

License
-------

This code is open source software licensed under the [Apache 2.0 License].

[GMP]: https://en.wikipedia.org/wiki/Guaranteed_Minimum_Pension

[Scala]: http://www.scala-lang.org/
[Play]: http://playframework.com/
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html

[Government Gateway]: http://www.gateway.gov.uk/

[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[HoD]: http://webarchive.nationalarchives.gov.uk/+/http://www.hmrc.gov.uk/manuals/sam/samglossary/samgloss249.htm
[JSON]: http://json.org/
[NINO]:https://www.gov.uk/national-insurance/your-national-insurance-number
[SCON]:https://www.gov.uk/payroll-pension-scheme
[PSA]: https://www.gov.uk/topic/business-tax/pension-scheme-administration
[SPA]: https://www.gov.uk/state-pension-age
[NPS]: http://www.publications.parliament.uk/pa/cm201012/cmselect/cmtreasy/731/73107.htm
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator

[Apache 2.0 License]: http://www.apache.org/licenses/LICENSE-2.0.html
