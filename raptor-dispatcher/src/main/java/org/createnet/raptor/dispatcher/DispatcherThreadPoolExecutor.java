/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.dispatcher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class DispatcherThreadPoolExecutor extends ThreadPoolExecutor {
  
  final private Logger logger = LoggerFactory.getLogger(DispatcherThreadPoolExecutor.class);
  
  public DispatcherThreadPoolExecutor(
          int corePoolSize, int maximumPoolSize, long keepAliveTime, 
            TimeUnit unit, BlockingQueue<Runnable> workQueue) {

    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
//    logger.debug("Perform beforeExecute() logic");
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    
    if (t != null) {
//      logger.debug("Perform exception handler logic");
    }
    
//    logger.debug("Perform afterExecute() logic");
  }

}
