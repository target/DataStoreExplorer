package com.target.datastorexplorer.ui.proto

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.target.datastorexplorer.coroutine.CoroutineDispatchers
import com.target.datastorexplorer.localization.DataStoreBundle
import com.target.datastorexplorer.ui.ProgressPanel
import com.target.datastorexplorer.provider.proto.ProtoEditor
import com.target.datastorexplorer.ui.JsonTreeLoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class ProtoScreen(
	project: Project,
	protoBuffFile: VirtualFile,
	dispatchers: CoroutineDispatchers
) : Disposable {

	private val presenter = ProtoScreenPresenter(project, protoBuffFile, dispatchers)

	private val scope = CoroutineScope(dispatchers.main())

	init {
		Disposer.register(ProtoEditor.parentDisposable, this)
		scope.launch {
			presenter.stateFlow().collectLatest {
				jbList.setListData(it.protoFile.map { it.name }.toTypedArray())
				if (it.treeModel != null) {
					jTree.model = it.treeModel
				}
				when (it.loadingState) {
					is JsonTreeLoadingState.Loading -> progressPanel.start()
					is JsonTreeLoadingState.NotLoading -> progressPanel.stop()
					null -> {}
				}
			}
		}
		presenter.init()
	}

	private val jbList = JBList<String>()
	private val jTree = Tree()
	private val progressPanel = ProgressPanel()

	fun protoBuffPanel(): JComponent {
		val protoBuffPanel = JPanel(BorderLayout()).apply {
			border = BorderFactory.createTitledBorder(DataStoreBundle.message("proto.select"))
		}
		val jbScrollPane = JBScrollPane().apply {
			setViewportView(add(jbList))
		}
		protoBuffPanel.add(jbScrollPane)

		val treePanel = JPanel(BorderLayout()).apply {
			border = BorderFactory.createTitledBorder(DataStoreBundle.message("panel.border.json.tree"))
		}
		treePanel.add(progressPanel, BorderLayout.NORTH)

		val treeSubPanel = JPanel(BorderLayout())
		treeSubPanel.add(
			JButton().apply {
				isOpaque = false
				text = DataStoreBundle.message("panel.button.generate.or.refresh.tree")
				icon = AllIcons.Actions.Refresh
				addActionListener {
					presenter.onRefreshClick(index = jbList.selectedIndex)
				}
			},
			BorderLayout.NORTH
		)
		val jbScrollTreePanel = JBScrollPane().apply {
			setViewportView(add(jTree))
		}
		treeSubPanel.add(jbScrollTreePanel)

		treePanel.add(treeSubPanel)

		return JBSplitter(false, 0.5f).apply {
			firstComponent = protoBuffPanel
			secondComponent = treePanel
		}
	}

	override fun dispose() {
		scope.cancel()
	}
}

