# Copyright 1999-2005 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

inherit eutils subversion java-pkg

DESCRIPTION="File-sharing program that uses Java and ZeroConf"
HOMEPAGE="http://www.chungles.com"
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~x86"

RDEPEND=">=virtual/jre-1.4.2
		>=dev-java/swt-3.2_pre1
		=dev-java/jmdns-0.2"

DEPEND=">=virtual/jdk-1.4.2
		>=dev-java/swt-3.2_pre1
        dev-java/ant-core
		dev-util/subversion"

#REV_DATE="${PV##*_alpha}"

ESVN_REPO_URI="https://svn.sourceforge.net/svnroot/chungles"
#ESVN_OPTIONS="-r {${REV_DATE}}"
ESVN_OPTIONS="-r 50"

src_unpack()
{
        subversion_src_unpack
		cd ${S}/chungles/trunk/lib
		java-pkg_jarfrom swt-3
}

src_compile()
{
		cd ${S}/chungles/trunk
		ant
}

src_install()
{
		cd ${S}/chungles/trunk/lib
		java-pkg_dojar chungles.jar
		CHUNGLES_DIR="${jardest}/chungles.jar"
		echo "CLASSPATH=${CHUNGLES_DIR}:$(java-pkg_getjars swt-3,jmdns) java -Djava.library.path=${DESTTREE}/lib Main" > chungles
		dobin chungles
}
