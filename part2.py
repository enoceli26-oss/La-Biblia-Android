import os

files = {
"app/src/main/res/layout/activity_main.xml": '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://android.com" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:fitsSystemWindows="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:padding="10dp" android:gravity="center_vertical">
        <Spinner android:id="@+id/spinnerLibro" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="2" />
        <Spinner android:id="@+id/spinnerCapitulo" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1" android:layout_marginStart="6dp" />
        <ImageButton android:id="@+id/btnSettings" android:layout_width="44dp" android:layout_height="44dp" android:layout_marginStart="6dp" android:background="?attr/selectableItemBackgroundBorderless" android:contentDescription="Ajustes" android:src="@android:drawable/ic_menu_preferences" />
    </LinearLayout>
    <androidx.recyclerview.widget.RecyclerView android:id="@+id/recyclerVersiculos" android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1" android:padding="12dp" android:clipToPadding="false" />
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="10dp">
        <TextView android:id="@+id/tvEstadoLectura" android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center" android:text="Toca Play para escuchar" android:textSize="12sp" android:paddingBottom="6dp" />
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center">
            <Button android:id="@+id/btnAnterior" style="@style/Widget.Material3.Button.OutlinedButton" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="⏮ Ant" />
            <Button android:id="@+id/btnPlayPause" android:layout_width="140dp" android:layout_height="wrap_content" android:layout_marginHorizontal="12dp" android:text="▶ Play" />
            <Button android:id="@+id/btnSiguiente" style="@style/Widget.Material3.Button.OutlinedButton" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Sig ⏭" />
        </LinearLayout>
    </LinearLayout>
</LinearLayout>
''',
"app/src/main/res/layout/item_versiculo.xml": '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://android.com" android:id="@+id/containerVersiculo" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="10dp" android:layout_marginBottom="4dp">
    <TextView android:id="@+id/tvTextoVersiculo" android:layout_width="match_parent" android:layout_height="wrap_content" android:lineSpacingExtra="4dp" android:textSize="18sp" />
</LinearLayout>
''',
"app/src/main/res/layout/activity_settings.xml": '''<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://android.com" android:layout_width="match_parent" android:layout_height="match_parent" android:fitsSystemWindows="true">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical" android:padding="20dp">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:paddingBottom="16dp">
            <Button android:id="@+id/btnVolver" style="@style/Widget.Material3.Button.TextButton" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="← Volver" />
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Ajustes de Lectura y Voz" android:textSize="20sp" android:textStyle="bold" android:layout_marginStart="8dp" />
        </LinearLayout>
        <TextView android:id="@+id/tvTamanoLabel" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Tamaño de letra: 18 sp" android:textStyle="bold" />
        <SeekBar android:id="@+id/seekTamano" android:layout_width="match_parent" android:layout_height="wrap_content" android:max="16" android:progress="4" android:paddingVertical="8dp" />
        <TextView android:id="@+id/tvEjemploLetra" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="1. En el principio creó Dios los cielos y la tierra." android:padding="10dp" android:textSize="18sp" />
        <com.google.android.material.switchmaterial.SwitchMaterial android:id="@+id/switchModoOscuro" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Modo oscuro activado" android:checked="true" android:paddingVertical="12dp" />
        <TextView android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Seleccionar Voz (Español)" android:textStyle="bold" android:paddingTop="12dp" android:paddingBottom="6dp" />
        <Spinner android:id="@+id/spinnerVoces" android:layout_width="match_parent" android:layout_height="48dp" />
        <TextView android:id="@+id/tvVelocidadLabel" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Velocidad de lectura: 1.0x" android:textStyle="bold" android:paddingTop="14dp" />
        <SeekBar android:id="@+id/seekVelocidad" android:layout_width="match_parent" android:layout_height="wrap_content" android:max="15" android:progress="5" android:paddingVertical="8dp" />
        <TextView android:id="@+id/tvTonoLabel" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Tono de voz: 1.0" android:textStyle="bold" android:paddingTop="10dp" />
        <SeekBar android:id="@+id/seekTono" android:layout_width="match_parent" android:layout_height="wrap_content" android:max="10" android:progress="5" android:paddingVertical="8dp" />
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:paddingVertical="12dp">
            <Button android:id="@+id/btnProbarVoz" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="🔊 Probar voz" />
            <Button android:id="@+id/btnMasVoces" style="@style/Widget.Material3.Button.OutlinedButton" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="8dp" android:text="⬇ Más voces" />
        </LinearLayout>
    </LinearLayout>
</ScrollView>
'''
}
for p, c in files.items():
    os.makedirs(os.path.dirname(p) or ".", exist_ok=True)
    open(p, "w", encoding="utf-8").write(c)
print("PART2_OK")
