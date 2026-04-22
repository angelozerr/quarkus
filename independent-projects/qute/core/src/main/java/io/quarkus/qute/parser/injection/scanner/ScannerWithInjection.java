package io.quarkus.qute.parser.injection.scanner;

import io.quarkus.qute.parser.injection.InjectionMetadata;
import io.quarkus.qute.parser.scanner.Scanner;

public interface ScannerWithInjection<T, S> extends Scanner<T, S> {

    InjectionMetadata getInjectionMetadata();

}
