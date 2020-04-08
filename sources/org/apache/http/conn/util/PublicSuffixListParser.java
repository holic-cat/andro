package org.apache.http.conn.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;

@Contract(threading = ThreadingBehavior.IMMUTABLE)
public final class PublicSuffixListParser {
    public final PublicSuffixList parse(Reader reader) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        BufferedReader bufferedReader = new BufferedReader(reader);
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                return new PublicSuffixList(DomainType.UNKNOWN, arrayList, arrayList2);
            }
            if (!readLine.isEmpty() && !readLine.startsWith("//")) {
                if (readLine.startsWith(".")) {
                    readLine = readLine.substring(1);
                }
                boolean startsWith = readLine.startsWith("!");
                if (startsWith) {
                    readLine = readLine.substring(1);
                }
                if (startsWith) {
                    arrayList2.add(readLine);
                } else {
                    arrayList.add(readLine);
                }
            }
        }
    }

    public final List<PublicSuffixList> parseByType(Reader reader) {
        ArrayList arrayList = new ArrayList(2);
        BufferedReader bufferedReader = new BufferedReader(reader);
        DomainType domainType = null;
        ArrayList arrayList2 = null;
        ArrayList arrayList3 = null;
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                return arrayList;
            }
            if (!readLine.isEmpty()) {
                if (readLine.startsWith("//")) {
                    if (domainType == null) {
                        if (readLine.contains("===BEGIN ICANN DOMAINS===")) {
                            domainType = DomainType.ICANN;
                        } else if (readLine.contains("===BEGIN PRIVATE DOMAINS===")) {
                            domainType = DomainType.PRIVATE;
                        }
                    } else if (readLine.contains("===END ICANN DOMAINS===") || readLine.contains("===END PRIVATE DOMAINS===")) {
                        if (arrayList2 != null) {
                            arrayList.add(new PublicSuffixList(domainType, arrayList2, arrayList3));
                        }
                        domainType = null;
                        arrayList2 = null;
                        arrayList3 = null;
                    }
                } else if (domainType != null) {
                    if (readLine.startsWith(".")) {
                        readLine = readLine.substring(1);
                    }
                    boolean startsWith = readLine.startsWith("!");
                    if (startsWith) {
                        readLine = readLine.substring(1);
                    }
                    if (startsWith) {
                        if (arrayList3 == null) {
                            arrayList3 = new ArrayList();
                        }
                        arrayList3.add(readLine);
                    } else {
                        if (arrayList2 == null) {
                            arrayList2 = new ArrayList();
                        }
                        arrayList2.add(readLine);
                    }
                }
            }
        }
    }
}
