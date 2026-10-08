package com.example

import com.example.camera.PoseCatalog
import com.example.data.model.CaptureMode
import com.example.data.model.SceneType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPoseCatalog() {
    val poses = PoseCatalog.poses
    assertTrue("Should have pose guides available", poses.isNotEmpty())
    val chinRest = poses.firstOrNull { it.id == "pose_chin_rest" }
    assertNotNull(chinRest)
    assertEquals("سيلفي يد على الذقن", chinRest?.titleAr)
  }

  @Test
  fun testCaptureModes() {
    val modes = CaptureMode.entries
    assertEquals(6, modes.size)
    assertTrue(modes.contains(CaptureMode.NIGHT))
    assertTrue(modes.contains(CaptureMode.HDR_MERGE))
    assertTrue(modes.contains(CaptureMode.PRO))
  }
}
