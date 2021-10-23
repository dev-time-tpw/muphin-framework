package de.devtime.muphin.core.phase;

import de.devtime.muphin.core.annotation.Phase;

/**
 * Serves only as dummy phase for the {@link Phase} annotation to make the parameters for the phases optional.
 *
 * @author morrigan
 * @since 0.0.1
 */
public class NoPhase extends AbstractPhase {

  /**
   * Creates a new instance of this phase.
   *
   * @since 0.0.1
   */
  public NoPhase() {
    super(AbstractPhase.INTERNAL_KIND, NoPhase.class.getSimpleName());
  }

  // checkstyle:WriteTag OFF

  @Override
  public boolean execute() {
    return true;
  }

  // checkstyle:WriteTag ON
}
