package utils
import github.gphat.censorinus.DogStatsDClient

object Metrics {
  def client = new DogStatsDClient()
}
