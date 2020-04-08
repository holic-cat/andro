package org.apache.http.impl.cookie;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;

@Contract(threading = ThreadingBehavior.IMMUTABLE)
public abstract class AbstractCookieAttributeHandler implements CookieAttributeHandler {
    public void validate(Cookie cookie, CookieOrigin cookieOrigin) {
    }

    public boolean match(Cookie cookie, CookieOrigin cookieOrigin) {
        return true;
    }
}
