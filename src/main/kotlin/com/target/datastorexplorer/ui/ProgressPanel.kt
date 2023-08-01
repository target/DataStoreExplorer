package com.target.datastorexplorer.ui

import com.intellij.openapi.progress.util.ColorProgressBar
import com.intellij.ui.SimpleColoredComponent
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JProgressBar

class ProgressPanel : JPanel() {
	private val progressBar = JProgressBar()
	private val state = object : SimpleColoredComponent() {
		override fun getMinimumSize(): Dimension {
			return Dimension(0, 0)
		}
	}

	init {
		progressBar.foreground = ColorProgressBar.GREEN
		progressBar.isIndeterminate = true
		isVisible = false

		val layout = BorderLayout(0, 0)
		setLayout(layout)
		add(state, BorderLayout.NORTH)
		add(progressBar, BorderLayout.CENTER)
	}

	fun start() {
		isVisible = true
	}

	fun stop() {
		isVisible = false
	}
}