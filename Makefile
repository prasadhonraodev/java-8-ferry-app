
.PHONY: create-golden-master
create-golden-master:
	./gradlew test --tests '*GoldenMaster.creation'

#--stacktrace --debug


.PHONY: check-golden-master
check-golden-master:
	./gradlew test --tests '*GoldenMasterTest'

.PHONY: test
test:
	./gradlew test

.PHONY: package
package:
	./gradlew fatjar
	mv ./build/libs/booking-all-*.jar ./build

.PHONY: execute
execute: package
	java -jar ./build/booking-all-*.jar


