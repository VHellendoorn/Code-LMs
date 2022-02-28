import initializeSession from "./core.js";
import type { SessionOptions, Session } from "./types";
import type { IncomingMessage, ServerResponse } from "http";

declare global {
  namespace Express {
    interface Request {
      session: Session<SessionData>;
    }
  }
  namespace Polka {
    interface Request {
      session: Session<SessionData>;
    }
  }
}

/**
 * This interface allows you to declare additional properties on your session object using [declaration merging](https://www.typescriptlang.org/docs/handbook/declaration-merging.html).
 *
 * @example
 * declare module 'svelte-kit-cookie-session' {
 *     interface SessionData {
 *         views: number;
 *     }
 * }
 *
 */
interface SessionData {
  [key: string]: any;
}

export function sessionMiddleware<
  Req extends { headers: IncomingMessage["headers"] },
  Res extends ServerResponse,
  SessionType = Record<string, any>
>(options: SessionOptions): (req: Req, res: Res, next: () => void) => any {
  return (req, res, next) => {
    const session: any = initializeSession<SessionType>(
      req.headers.cookie || '',
      options
    );

    //@ts-ignore
    req.session = session;

    const setSessionHeaders = () => {
      //@ts-ignore This can exist
      const sessionCookie = req.session["set-cookie"];

      if (sessionCookie && sessionCookie.length > 0) {
        const existingSetCookie = res.getHeader("Set-Cookie") as
          | string
          | string[];

        if (!existingSetCookie) {
          res.setHeader("Set-Cookie", [sessionCookie]);
        } else if (typeof existingSetCookie === "string") {
          res.setHeader("Set-Cookie", [existingSetCookie, sessionCookie]);
        } else {
          res.setHeader("Set-Cookie", [...existingSetCookie, sessionCookie]);
        }
      }
    };

    const end = res.end;
    res.end = function (...args: any) {
      setSessionHeaders();
      return end.apply(this, args);
    };

    return next();
  };
}
