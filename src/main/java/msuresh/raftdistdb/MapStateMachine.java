/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.StateMachineExecutor;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author muthukumarsuresh
 */
public class MapStateMachine extends StateMachine{
  private final Map<Object, Commit<PutCommand>> map = new HashMap<>();
  
  @Override
  protected void configure(StateMachineExecutor executor) {
    executor.register(RemoveCommand.class, this::remove);
    executor.register(PutCommand.class, this::put);
    executor.register(GetQuery.class, this::get);
    
  }

  private Object put(Commit<PutCommand> commit) {
    // Store the full commit object in the map to ensure we can properly clean it from the commit log once we're done.
    map.put(commit.operation().key(), commit);
    return 1;
  }

  private Object get(Commit<GetQuery> commit) {
    try {
      // Get the commit value and return the operation value if available.
      Commit<PutCommand> value = map.get(commit.operation().key());
      return value != null ? value.operation().value() : null;
    } finally {
      // Close the query commit once complete to release it back to the internal commit pool.
      // Failing to do so will result in warning messages.
      commit.close();
    }
  }

  private Object remove(Commit<RemoveCommand> commit) {
    try {
      // Remove the commit with the given key.
      Commit<PutCommand> value = map.remove(commit.operation().key());

      // If a commit with the given key existed, get the result and then clean the commit from the log.
      if (value != null) {
        Object result = value.operation().value();
        value.clean();
        return result;
      }
      return null;
    } finally {
      // Finally, clean the remove commit.
      commit.clean();
    }
  }
}

