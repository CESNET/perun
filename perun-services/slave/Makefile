VERSION = x.x

default: deb rpm

deb:
	dpkg-buildpackage -us -uc

rpm:
	./prepare_rpm_spec_file.sh

clean:
	rm -rf debian/tmp
	rm -f ../perun-slave_*
	rm -f ../perun-slave-*.rpm
