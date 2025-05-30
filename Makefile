
.PHONY: build-kt build-vscode install

build-kt:
	@echo "Building Kotlin project..."
	mvn install
	@echo "Kotlin project built successfully."

build-vscode: build-kt
	@echo "Building VSCode extension..."
	cd vscode && vsce package
	@echo "VSCode extension built successfully."

install: build-vscode
	cd vscode && code --install-extension sv-comp-verifiers-0.0.1.vsix

NATIVE_BUILD_PARAMS := -O3
# Uncomment for faster builds during development.
# -Ob enables build optimizations to speed up the build
# (at the cost of runtime performance of the produced executable).
# NATIVE_BUILD_PARAMS := -Ob
.PHONY: cpachecker
cpachecker: build/cpachecker/cpachecker.jar
	build/collect-dependencies.sh
	build/build-native.sh build/cpachecker build/META-INF build/native-build ${NATIVE_BUILD_PARAMS}
	[ -d lib/cpachecker-native ] || mkdir -p lib/cpachecker-native
	mv build/native-build/cpachecker lib/cpachecker-native/
	[ ! -d lib/cpachecker-native/config ] || rm -r lib/cpachecker-native/config
	cp -r build/cpachecker/config lib/cpachecker-native/config
	@echo "Cpachecker native build completed successfully."

# .PHONY: build/cpachecker/cpachecker.jar
build/cpachecker/cpachecker.jar:
	git submodule update --init build/cpachecker
	cd build/cpachecker && ant jar
