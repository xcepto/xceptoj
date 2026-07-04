package org.xcepto.xceptoj.sequential.services;

public class StatefulService {
  public enum State {
    A, B, C
  }

  private State state = State.A;

  public State getState() {
    return state;
  }

  public void advance() {
    switch (state) {
      case A -> state = State.B;
      case B -> state = State.C;
      case C -> { }
    }
  }
}
