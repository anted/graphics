package se.kth.livetech.contest.model.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import se.kth.livetech.contest.model.Attrs;
import se.kth.livetech.contest.model.AttrsUpdateEvent;
import se.kth.livetech.contest.model.Contest;
import se.kth.livetech.contest.model.Judgement;

public class AttrsUpdateEventImpl implements AttrsUpdateEvent {
	long time;
	String type;
	Map<String, String> update;

	public AttrsUpdateEventImpl(long time, String type) {
		this.time = time;
		this.type = type;
		update = new LinkedHashMap<String, String>();
	}

	public long getTime() {
		return time;
	}

	public String getType() {
		return type;
	}

	public Set<String> getProperties() {
		return update.keySet();
	}

	public String getProperty(String name) {
		return update.get(name);
	}

	public void setProperty(String name, String value) {
		update.put(name, value);
	}

	private Map<String, String> mergeProperties(Attrs attrs) {
		Map<String, String> merge = new LinkedHashMap<String, String>();
		if (attrs != null)
			for (String name : attrs.getProperties())
				merge.put(name, attrs.getProperty(name));
		for (String name : update.keySet())
			merge.put(name, update.get(name));
		return merge;
	}

	public Attrs merge(Contest contest) {
		Attrs attrs;
		if (type.equals("info"))
			attrs = new InfoImpl(mergeProperties(contest.getInfo()));
		else if (type.equals("language")) {
			String id = update.get("name");
			attrs = new LanguageImpl(mergeProperties(contest.getLanguage(id)));
		} else if (type.equals("judgement")) {
			Judgement j = new JudgementImpl(mergeProperties(null));
			// String id = update.get("acronym");
			String id = j.getAcronym();
			attrs = new JudgementImpl(mergeProperties(contest.getJudgement(id)));
		} else {
			int id = 0;
			if (update.containsKey("id")) // TODO:!!
				id = Integer.valueOf(update.get("id"));
			if (type.equals("problem"))
				attrs = new ProblemImpl(mergeProperties(contest.getProblem(id)));
			else if (type.equals("team"))
				attrs = new TeamImpl(mergeProperties(contest.getTeam(id)));
			else if (type.equals("run"))
				attrs = new RunImpl(mergeProperties(contest.getRun(id)));
			else if (type.equals("clar"))
				attrs = new ClarImpl(mergeProperties(contest.getClar(id)));
			else {
				new Error("Unknown type " + type).printStackTrace();
				attrs = new AttrsImpl(update);
			}
		}
		return attrs;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(type);
		s.append('{');
		for (String p : getProperties()) {
			if (!p.equals("id"))
				s.append(", ");
			s.append(p);
			s.append('=');
			s.append(getProperty(p));
		}
		s.append('}');
		return s.toString();
	}
}