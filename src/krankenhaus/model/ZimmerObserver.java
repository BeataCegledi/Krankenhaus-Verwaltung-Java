package krankenhaus.model;

@FunctionalInterface
public interface ZimmerObserver {
	void aktualisiere(ZimmerAenderung zA);
}
