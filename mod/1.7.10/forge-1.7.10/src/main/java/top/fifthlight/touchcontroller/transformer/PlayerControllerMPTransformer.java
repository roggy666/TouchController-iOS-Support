package top.fifthlight.touchcontroller.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PlayerControllerMPTransformer extends TouchControllerClassVisitor {
    public PlayerControllerMPTransformer(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public String getClassName() {
        return "net.minecraft.client.multiplayer.PlayerControllerMP";
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 1.7.10: onPlayerRightClick (func_78760_a) - handles both block and item use
        boolean isOnPlayerRightClick = "onPlayerRightClick".equals(name) || "func_78760_a".equals(mapSelfMethodName(name, desc));
        // 1.7.10: sendUseItem (func_78769_a) - sends use item packet
        boolean isSendUseItem = "sendUseItem".equals(name) || "func_78769_a".equals(mapSelfMethodName(name, desc));

        if (isOnPlayerRightClick || isSendUseItem) {
            return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                // Call PlayerControllerMPHelper.beforeUsingItem at the head of method
                @Override
                public void visitCode() {
                    // Load this (PlayerControllerMP) and player (EntityPlayer)
                    visitVarInsn(Opcodes.ALOAD, 0);
                    visitVarInsn(Opcodes.ALOAD, 1);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "top/fifthlight/touchcontroller/helper/PlayerControllerMPHelper", "beforeUsingItem", "(Lnet/minecraft/client/multiplayer/PlayerControllerMP;Lnet/minecraft/entity/player/EntityPlayer;)V", false);
                    super.visitCode();
                }

                // Call PlayerControllerMPHelper.afterUsingItem before every return
                @Override
                public void visitInsn(int opcode) {
                    if (opcode == Opcodes.IRETURN || opcode == Opcodes.RETURN) {
                        visitVarInsn(Opcodes.ALOAD, 0);
                        visitVarInsn(Opcodes.ALOAD, 1);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, "top/fifthlight/touchcontroller/helper/PlayerControllerMPHelper", "afterUsingItem", "(Lnet/minecraft/client/multiplayer/PlayerControllerMP;Lnet/minecraft/entity/player/EntityPlayer;)V", false);
                    }
                    super.visitInsn(opcode);
                }
            };
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
