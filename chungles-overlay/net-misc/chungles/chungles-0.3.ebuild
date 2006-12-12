# Copyright 1999-2005 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

inherit eutils java-pkg-2 java-ant-2

DESCRIPTION="File-sharing program that uses Java and ZeroConf"
HOMEPAGE="http://www.chungles.com"
SRC_URI="mirror://sourceforge/${PN}/${P}.tar.gz"
LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~x86"
IUSE=""

RDEPEND=">=virtual/jre-1.5.0
		>=dev-java/swt-3.2_pre1
		=dev-java/jmdns-0.2"

DEPEND=">=virtual/jdk-1.5.0
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
		eant
}

src_install()
{
		cd ${S}/lib
		java-pkg_dojar chungles.jar
		java-pkg_dolauncher "chungles" --java_args "-classpath $(java-pkg_getjars swt-3,jmdns):${JAVA_PKG_JARDEST}/chungles.jar -Djava.library.path=${DESTTREE}/lib" --main "org.chungles.application.Main" --jar ""
		doicon ${S}/images/${PN}.png
		make_desktop_entry ${PN} Chungles ${PN}.png
}
