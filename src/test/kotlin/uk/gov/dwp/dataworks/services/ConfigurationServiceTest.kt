package uk.gov.dwp.dataworks.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@WebMvcTest(ConfigurationService::class)
class ConfigurationServiceTest {
    @Autowired
    private lateinit var configService: ConfigurationService

    @After
    fun removeEnvVars() {
        ConfigKey.values().forEach { System.clearProperty(it.key) }
        configService.clear()
    }

    @Test
    fun `Can retrieve environment variables`() {
        setSystemTestVars()
        ConfigKey.values().forEach {
            if (it.isList) {
                val expected = "${it.key}-testValue1,${it.key}-testValue2".split(",")
                val actual = configService.getListConfig(it)
                assertThat(actual).isEqualTo(expected)
            } else {
                val expected = "${it.key}-testValue"
                val actual = configService.getStringConfig(it)
                assertThat(actual).isEqualTo(expected)
            }
        }
    }

    @Test
    fun `Throws exception when List variable cannot be found`() {
        ConfigKey.values()
                .filter { it.isList }
                .forEach {
                    assertThatCode { configService.getListConfig(it) }
                            .hasMessage("No value found for ${it.key}")
                }
    }

    @Test
    fun `Throws exception when String variable cannot be found`() {
        ConfigKey.values()
                .filter { !it.isList }
                .forEach {
                    assertThatCode { configService.getStringConfig(it) }
                            .hasMessage("No value found for ${it.key}")
                }
    }

    fun setSystemTestVars() {
        ConfigKey.values().forEach {
            val testValue = if (it.isList) "${it.key}-testValue1,${it.key}-testValue2" else "${it.key}-testValue"
            System.setProperty(it.key, testValue)
        }
    }
}
