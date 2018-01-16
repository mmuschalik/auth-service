package Aggregates

trait RepoTrait {
  def setId[T <: Entity](mt: T, int: Int): Unit = {
    mt.setId(int)
  }
}
