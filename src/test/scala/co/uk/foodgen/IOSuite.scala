package co.uk.foodgen

import weaver.SimpleIOSuite

trait IOSuite extends SimpleIOSuite:
  override def maxParallelism: Int = 3
end IOSuite
