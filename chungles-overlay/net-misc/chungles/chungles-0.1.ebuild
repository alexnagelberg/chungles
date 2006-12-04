# Copyright 1999-2005 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

inherit eutils java-pkg

DESCRIPTION="File-sharing program that uses Java and ZeroConf"
HOMEPAGE="http://www.chungles.com"
SRC_URI="mirror://sourceforge/${PN}/${P}.tar.gz"
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~x86"

RDEPEND=">=virtual/jre-1.4.2
		>=dev-java/swt-3.2_pre1
		=dev-java/jmdns-0.2"

DEPEND=">=virtual/jdk-1.4.2
		>=dev-java/swt-3.2_pre1
        dev-java/ant-core"

src_unpack()
{
		unpack ${A} || die
		cd ${S}/lib
		java-pkg_jarfrom swt-3
}

src_compile()
{
		cd ${S}
		ant || die
}

src_install()
{
		cd ${S}/lib
		java-pkg_dojar chungles.jar
		CHUNGLES_DIR="${jardest}/chungles.jar"
		echo "CLASSPATH=${CHUNGLES_DIR}:$(java-pkg_getjars swt-3,jmdns) java -Djava.library.path=${DESTTREE}/lib Main" > ${PN}
		dobin ${PN}
		doicon ${S}/images/${PN}.png
		make_desktop_entry ${PN} Chungles ${PN}.png
}
