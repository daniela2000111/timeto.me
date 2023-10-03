import SwiftUI
import shared

struct NoteFormSheet: View {

    @EnvironmentObject private var nativeSheet: NativeSheet

    @Binding private var isPresented: Bool
    @State private var vm: NoteFormSheetVM
    private let onDelete: () -> Void

    init(
            isPresented: Binding<Bool>,
            note: NoteModel?,
            onDelete: @escaping () -> Void
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: NoteFormSheetVM(note: note))
        self.onDelete = onDelete
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                    title: state.headerTitle,
                    scrollToHeader: 0,
                    bgColor: c.sheetBg
            )

            MyListView__ItemView(
                    isFirst: true,
                    isLast: true
            ) {

                MyListView__ItemView__TextInputView(
                        text: state.inputTextValue,
                        placeholder: state.inputTextPlaceholder,
                        isAutofocus: false,
                        onValueChanged: { newText in
                            vm.setInputText(newText: newText)
                        }
                )
            }
                    .padding(.bottom, H_PADDING)

            Spacer()

            Sheet__BottomViewDefault(
                    primaryText: state.doneTitle,
                    primaryAction: {
                        vm.save {
                            isPresented = false
                        }
                    },
                    secondaryText: "Cancel",
                    secondaryAction: {
                        isPresented = false
                    },
                    topContent: { EmptyView() },
                    startContent: {

                        ZStack {


                        }
                    }
            )
        }
    }
}
