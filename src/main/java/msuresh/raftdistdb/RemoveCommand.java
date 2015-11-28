/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import io.atomix.copycat.client.Command;

/**
 *
 * @author muthukumarsuresh
 */
public class RemoveCommand implements Command<Object> {
  private final Object key;

  public RemoveCommand(Object key) {
    this.key = key;
  }

  @Override
  public PersistenceLevel persistence() {
    return PersistenceLevel.PERSISTENT;
  }

  public Object key() {
    return key;
  }
}

