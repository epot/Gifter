package utils

import com.timgroup.statsd.NonBlockingStatsDClient


object Metrics {
  def client = new NonBlockingStatsDClient("")
}
