SUMMARY = "Pre-Built Amlogic FIP Binaries distributed in U-Boot"
PROVIDES = "virtual/trusted-firmware-a"

LICENSE = "Proprietary"

DEPENDS = "u-boot python3-native"

PACKAGE_ARCH = "${MACHINE_ARCH}"

# Building on non-x86_64 requires using gxlimg
PACKAGECONFIG ??= "${@bb.utils.contains('SDK_ARCH', 'x86_64', '', 'gxlimg', d)}"
PACKAGECONFIG[gxlimg] = "gxlimg,,gxlimg-native"

COMPATIBLE_MACHINE = ""
COMPATIBLE_MACHINE:libretech-cc = "libretech-cc"
COMPATIBLE_MACHINE:libretech-ac = "libretech-ac"
COMPATIBLE_MACHINE:khadas-vim = "khadas-vim"
COMPATIBLE_MACHINE:khadas-vim2 = "khadas-vim2"
COMPATIBLE_MACHINE:khadas-vim3 = "khadas-vim3"
COMPATIBLE_MACHINE:khadas-vim3l = "khadas-vim3l"
COMPATIBLE_MACHINE:amlogic-p200 = "amlogic-p200"
COMPATIBLE_MACHINE:amlogic-p201 = "amlogic-p201"
COMPATIBLE_MACHINE:amlogic-p212 = "amlogic-p212"
COMPATIBLE_MACHINE:friendlyelec-nanopik2 = "friendlyelec-nanopik2"
COMPATIBLE_MACHINE:amlogic-s400 = "amlogic-s400"
COMPATIBLE_MACHINE:hardkernel-odroidc2 = "hardkernel-odroidc2"
COMPATIBLE_MACHINE:hardkernel-odroidc4 = "hardkernel-odroidc4"
COMPATIBLE_MACHINE:hardkernel-odroidhc4 = "hardkernel-odroidhc4"
COMPATIBLE_MACHINE:hardkernel-odroidn2 = "hardkernel-odroidn2"
COMPATIBLE_MACHINE:hardkernel-odroidn2plus = "hardkernel-odroidn2plus"
COMPATIBLE_MACHINE:seirobotics-sei510 = "seirobotics-sei510"
COMPATIBLE_MACHINE:seirobotics-sei610 = "seirobotics-sei610"

MODEL:libretech-cc = "lepotato"
MODEL:libretech-ac = "lafrite"
MODEL:khadas-vim = "khadas-vim"
MODEL:khadas-vim2 = "khadas-vim2"
MODEL:khadas-vim3 = "khadas-vim3"
MODEL:khadas-vim3l = "khadas-vim3l"
MODEL:amlogic-p200 = "p200"
MODEL:amlogic-p201 = "p201"
MODEL:amlogic-p212 = "p212"
MODEL:friendlyelec-nanopik2 = "nanopi-k2"
MODEL:amlogic-s400 = "s400"
MODEL:hardkernel-odroidc2 = "odroid-c2"
MODEL:hardkernel-odroidc4 = "odroid-c4"
MODEL:hardkernel-odroidhc4 = "odroid-hc4"
MODEL:hardkernel-odroidn2 = "odroid-n2"
MODEL:hardkernel-odroidn2plus = "odroid-n2-plus"
MODEL:seirobotics-sei510 = "sei510"
MODEL:seirobotics-sei610 = "sei610"

LIC_FILES_CHKSUM = "file://LICENSE;md5=2dbd68496cc5ed3e68e855100cb86363"

SRC_URI = "git://github.com/LibreELEC/amlogic-boot-fip.git;protocol=https;branch=master \
           file://0001-Add-gxlimg-commands-if-GXLIMG_PATH-if-set.patch \
           "
SRCREV = "7ff0004e0e4d261ba81334a2f46302bd06704aca"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

inherit deploy

do_compile () {
	if [ ${PACKAGECONFIG_CONFARGS} == "gxlimg" ] ; then
		bbwarn "GXLIMG usage is experimental"
		export GXLIMG_PATH=$(which gxlimg)
	fi

	mkdir -p ${B} ${B}/tmp
	(cd ${S} ; ./build-fip.sh ${MODEL} ${DEPLOY_DIR_IMAGE}/u-boot.bin ${B} ${B}/tmp)

	if [ ${PACKAGECONFIG_CONFARGS} == "gxlimg" ] ; then
		bbwarn "GXLIMG doesn't output u-boot.bin.sd.bin file, using empty first sector"
		dd if=/dev/zero of=${B}/u-boot.bin.sd.bin bs=512 count=1
		cat ${B}/u-boot.bin >> ${B}/u-boot.bin.sd.bin
	fi
}
do_compile[depends] += "u-boot:do_deploy"
do_compile[cleandirs] = "${B}"

do_deploy () {
    install ${B}/u-boot.bin.sd.bin ${DEPLOYDIR}/u-boot.bin.sd.bin
    install ${B}/u-boot.bin ${DEPLOYDIR}/u-boot.bin.mmc.bin
    install ${B}/u-boot.bin.usb.bl2 ${DEPLOYDIR}/u-boot.bin.usb.bl2
    install ${B}/u-boot.bin.usb.tpl ${DEPLOYDIR}/u-boot.bin.usb.tpl
}

addtask deploy after do_compile
