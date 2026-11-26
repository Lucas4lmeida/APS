package br.aps.fluxograma.model.regras;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Semestre letivo no formato "AAAA.S" (ex.: 2026.2). */
public record SemestreLetivo(int ano, int periodo) implements Comparable<SemestreLetivo> {

	private static final Pattern FORMATO = Pattern.compile("^(\\d{4})\\.([12])$");

	public SemestreLetivo {
		if (periodo != 1 && periodo != 2) {
			throw new IllegalArgumentException("Período deve ser 1 ou 2: " + periodo);
		}
	}

	public static SemestreLetivo parse(String texto) {
		Matcher m = FORMATO.matcher(texto == null ? "" : texto.trim());
		if (!m.matches()) {
			throw new IllegalArgumentException("Semestre letivo inválido (use AAAA.S): " + texto);
		}
		return new SemestreLetivo(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
	}

	public static boolean valido(String texto) {
		return texto != null && FORMATO.matcher(texto.trim()).matches();
	}

	/** Janeiro a junho = .1; julho a dezembro = .2. */
	public static SemestreLetivo daData(LocalDate data) {
		return new SemestreLetivo(data.getYear(), data.getMonthValue() <= 6 ? 1 : 2);
	}

	@Override
	public int compareTo(SemestreLetivo outro) {
		return Integer.compare(ano * 10 + periodo, outro.ano * 10 + outro.periodo);
	}

	@Override
	public String toString() {
		return ano + "." + periodo;
	}
}
