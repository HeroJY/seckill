package org.seckill.exception;

/**
 * 与秒杀相关的异常
 * @author hjy
 *
 */
public class SeckillException extends RuntimeException {

	public SeckillException(String message) {
		super(message);
	}

	public SeckillException(String message, Throwable cause) {
		super(message, cause);
	}
}
