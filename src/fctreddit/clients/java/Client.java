package fctreddit.clients.java;

import fctreddit.api.java.Result;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Invocation.Builder;

public abstract class Client {
    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;
    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;
    public Client() {

    }
    protected Response executeOperationGet(Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.get();
            } catch( ProcessingException x ) {
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ignored) {}
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return null;
    }

    protected Response executeOperationPost(Builder req, Entity<?> entity) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.post(entity);
            } catch( ProcessingException x ) {
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ignored) {}
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return null;
    }

    protected Response executeOperationPut(Builder req, Entity<?> entity) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.put(entity);
            } catch( ProcessingException x ) {
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ignored) {}
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return null;
    }

    protected Response executeOperationDelete(Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.delete();
            } catch( ProcessingException x ) {
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ignored) {}
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return null;
    }

    public static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
