package org.pm4j.demo.basic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MyDemoView extends Composite {

	private Label nameLabel = null;
	private Text myName = null;
	private Label textAreaLabel = null;
	private Text textArea = null;
	private Button button1 = null;

	public MyDemoView(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		GridData gridData21 = new GridData(GridData.FILL_BOTH);
		gridData21.horizontalSpan = 6;
		GridData gridData11 = new GridData();
		gridData11.horizontalSpan = 6;
		gridData11.verticalSpan = 2;
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 4;
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 4;
		GridData gridData = new GridData();
		nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("textLabel");
		nameLabel.setLayoutData(gridData1);
		myName = new Text(this, SWT.BORDER);
		myName.setLayoutData(gridData);
		textAreaLabel = new Label(this, SWT.NONE);
		textAreaLabel.setText("textAreaLabel");
		textAreaLabel.setLayoutData(gridData2);
		textArea = new Text(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		textArea.setLayoutData(gridData21);
		button1 = new Button(this, SWT.NONE);
		button1.setText("Button1");
		button1.setText("Button1");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.makeColumnsEqualWidth = true;
		this.setLayout(gridLayout);
		setSize(new Point(428, 263));
	}

	public Label getNameLabel() {
		return nameLabel;
	}

	public Text getMyName() {
		return myName;
	}

	public Label getTextAreaLabel() {
		return textAreaLabel;
	}

	public Text getTextArea() {
		return textArea;
	}

	public Button getButton1() {
		return button1;
	}

}  //  @jve:decl-index=0:visual-constraint="-8,6"
