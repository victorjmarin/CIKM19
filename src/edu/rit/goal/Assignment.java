package edu.rit.goal;

public enum Assignment {

	JOHNY("assignments/JOHNY"), CARVANS("assignments/CARVANS"), BUYING2("assignments/BUYING2"),
	MUFFINS3("assignments/MUFFINS3"), CLEANUP("assignments/CLEANUP"), CONFLIP("assignments/CONFLIP"),
	LAPIN("assignments/LAPIN"), PERMUT2("assignments/PERMUT2"), STONES("assignments/STONES"),
	SUMTRIAN("assignments/SUMTRIAN");

	public String pathToPrograms;

	private Assignment(String pathToPrograms) {
		this.pathToPrograms = pathToPrograms;
	}

}
