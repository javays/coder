package com.ivy.coder.dao;

import com.ivy.coder.utils.StringUtils;

public enum Cmd {
	
	validateNull("validateNull"),
	fillCriteria("fillcriteria"),
	pstmtSet("pstmtSet"),
	wrapRs("wrapRs"),
	updateSql("updatesql");
	
	private String cmd;
	
	private Cmd(String cmd) {
		this.cmd = cmd;
	}

	public String getCmd() {
		return cmd;
	}
	
	public static Cmd parseCmd(String cmd) {
		if (StringUtils.isEmpty(cmd)) {
			return null;
		}
		
		Cmd[] cmds = Cmd.values();
		for (Cmd _cmd : cmds) {
			if (_cmd.name().equals(cmd)) {
				return _cmd;
			}
		}
		return null;
	}
}
